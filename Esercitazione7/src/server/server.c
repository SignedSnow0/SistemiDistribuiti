#include "proto.h"
#include <dirent.h>
#include <sys/types.h>
#include <unistd.h>
#include <fcntl.h>
#include <netdb.h>

#define MAX_FILES 8
#define HOSTNAME "localhost"
#define BASE_PORT 5000
#define SOCKET_BACKLOG 5

int receive_string(int sock, char* string) {
    int length;
    if (read(sock, &length, sizeof(length)) <= 0) {
        return 0;
    }

    read(sock, string, length);

    return length;
}

Output* list_files_1_svc(Input* input, struct svc_req* req) {
    static Output output;

    DIR* dir;
    if (!(dir = opendir(input->directory))) {
        return 0;
    }

    struct dirent* entry;
    while ((entry = readdir(dir)) != 0 && output.num_files < MAX_FILES) {
        if (entry->d_type == DT_REG) {
            strcpy(output.files[output.num_files++].name, entry->d_name);
        }
    }

    closedir(dir);

    if (input->server_active) {
        int pid = fork();
        if (pid < 0) {
            return 0;
        }
        else if (pid == 0) {
            int sock;
            if ((sock = socket(AF_INET, SOCK_STREAM, 0)) < 0) {
                printf("Error: could not create socket\n");
                exit(1);
            }

            struct hostent* host;
            if (!(host = gethostbyname(input->client.hostname))) {
                printf("Error: could not resolve hostname\n");
                exit(1);
            }

            struct sockaddr_in address;
            address.sin_family = AF_INET;
            address.sin_port = htons(input->client.port);
            address.sin_addr.s_addr = ((struct in_addr*)host->h_addr_list[0])->s_addr;

            if ((connect(sock, (struct sockaddr*)&address, sizeof(address))) < 0) {
                printf("Error: could not connect to client\n");
                exit(1);
            }

            char buf[512];
            while (receive_string(sock, buf)) {
                int file;
                if ((file = open(buf, O_RDONLY)) < 0) {
                    int length = 0;
                    write(sock, &length, sizeof(length));

                    continue;
                }

                int length = lseek(file, 0, SEEK_END);
                lseek(file, 0, SEEK_SET);

                write(sock, &length, sizeof(length));

                while (length > 0) {
                    int n_read = read(file, buf, sizeof(buf));
                    write(sock, buf, n_read);
                    length -= n_read;
                }

                close(file);
            }

            close(sock);
            exit(0);
        }
    }
    else {
        int sock;
        if ((sock = socket(AF_INET, SOCK_STREAM, 0)) < 0) {
            return 0;
        }

        int port = BASE_PORT + sock;
        struct sockaddr_in address;
        address.sin_family = AF_INET;
        address.sin_port = htons(port);
        address.sin_addr.s_addr = INADDR_ANY;

        if (bind(sock, (struct sockaddr*)&address, sizeof(address)) < 0) {
            return 0;
        }

        if (listen(sock, 5) < 0) {
            return 0;
        }

        strcpy(output.server.hostname, HOSTNAME);
        output.server.port = port;

        int pid = fork();
        if (pid < 0) {
            return 0;
        }
        else if (pid == 0) {
            if ((sock = accept(sock, 0, 0)) < 0) {
                exit(1);
            }

            char buf[512];
            while (receive_string(sock, buf)) {
                int file;
                if ((file = open(buf, O_RDONLY)) < 0) {
                    continue;
                }

                int length = lseek(file, 0, SEEK_END);
                lseek(file, 0, SEEK_SET);

                write(sock, &length, sizeof(length));

                while (length > 0) {
                    int n_read = read(file, buf, sizeof(buf));
                    write(sock, buf, n_read);
                    length -= n_read;
                }

                close(file);
            }

            close(sock);
            exit(0);
        }
    }

    return &output;
}