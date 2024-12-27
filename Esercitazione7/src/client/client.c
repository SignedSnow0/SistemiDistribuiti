#include "proto.h"
#include <netdb.h>
#include <unistd.h>
#include <fcntl.h>
#include <string.h>

#define MAX_FILES 8
#define HOSTNAME "localhost"
#define BASE_PORT 6000
#define SOCKET_BACKLOG 5

void send_string(int sock, char* string) {
    int length = 0;
    while (string[length++]);

    write(sock, &length, sizeof(length));
    write(sock, string, length);
}

int main(int argc, char** argv) {
    if (argc != 2) {
        printf("Usage: %s <hostname>\n", argv[0]);
        return 1;
    }

    CLIENT* client;
    if (!(client = clnt_create(argv[1], MGET_PROG, MGET_VERS, "tcp"))) {
        printf("Error: could not connect to server\n");
    }

    Input input;
    printf("Enter directory: ");
    while (gets(input.directory)) {
        printf("Choose client mode (P = Passive, A = Active): ");
        char mode;
        scanf("%c", &mode);

        if (mode == 'P') {
            input.server_active = 1;

            int sock;
            if ((sock = socket(AF_INET, SOCK_STREAM, 0)) < 0) {
                continue;
            }

            int port = BASE_PORT + sock;
            struct sockaddr_in address;
            address.sin_family = AF_INET;
            address.sin_port = htons(port);
            address.sin_addr.s_addr = INADDR_ANY;

            if (bind(sock, (struct sockaddr*)&address, sizeof(address)) < 0) {
                close(sock);
                continue;
            }

            if (listen(sock, SOCKET_BACKLOG) < 0) {
                close(sock);
                continue;
            }

            strcpy(input.client.hostname, HOSTNAME);
            input.client.port = port;

            Output* output;
            if (!(output = list_files_1(&input, client))) {
                close(sock);
                printf("Enter directory: ");
                continue;
            }

            if ((sock = accept(sock, 0, 0)) < 0) {
                printf("Error: could not accept connection\n");
                close(sock);
                continue;
            }

            char buf[512];
            for (int i = 0; i < output->num_files; i++) {
                int file;
                if ((file = open(output->files[i].name, O_WRONLY | O_CREAT | O_TRUNC, 0644)) < 0) {
                    printf("Error: could not open file\n");
                    continue;
                }

                sprintf(buf, "%s/%s", input.directory, output->files[i].name);
                send_string(sock, buf);

                int length;
                if (!read(sock, &length, sizeof(length))) {
                    printf("Error: could not read from server\n");
                    close(sock);
                    break;
                }

                while (length > 0) {
                    int n_read = read(sock, buf, sizeof(buf));
                    write(file, buf, n_read);
                    length -= n_read;
                }

                close(file);
            }

            close(sock);
        }
        else if (mode == 'A') {
            input.server_active = 0;

            Output* output;
            if (!(output = list_files_1(&input, client))) {
                printf("Enter directory: ");
                continue;
            }

            int sock;
            if ((sock = socket(AF_INET, SOCK_STREAM, 0)) < 0) {
                printf("Error: could not create socket\n");
                continue;
            }

            struct hostent* host;
            if (!(host = gethostbyname(output->server.hostname))) {
                printf("Error: could not resolve hostname\n");
                continue;
            }

            struct sockaddr_in address;
            address.sin_family = AF_INET;
            address.sin_port = htons(output->server.port);
            address.sin_addr.s_addr = ((struct in_addr*)host->h_addr_list[0])->s_addr;

            if ((connect(sock, (struct sockaddr*)&address, sizeof(address))) < 0) {
                printf("Error: could not connect to server\n");
                continue;
            }

            char buf[512];
            for (int i = 0; i < output->num_files; i++) {
                int file;
                if ((file = open(output->files[i].name, O_WRONLY | O_CREAT | O_TRUNC, 0644)) < 0) {
                    printf("Error: could not open file\n");
                    break;
                }

                sprintf(buf, "%s/%s", input.directory, output->files[i].name);
                send_string(sock, buf);

                int length;
                read(sock, &length, sizeof(length));

                while (length > 0) {
                    int n_read = read(sock, buf, sizeof(buf));
                    write(file, buf, n_read);
                    length -= n_read;
                }

                close(file);
            }

            close(sock);
        }
        else {
            printf("Error: invalid mode\n");
            continue;
        }


        while (getchar() != '\n');
        printf("Enter directory: ");
    }
}