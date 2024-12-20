#include <dirent.h>
#include <fcntl.h>
#include <memory.h>
#include <netdb.h>
#include <netinet/in.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <unistd.h>

void send_string(int sock, const char *str) {
    int length = 0;
    while (str[length++])
        ;

    write(sock, &length, sizeof(length));
    write(sock, str, length);
    printf("Inviata stringa:\n\tLunghezza: %d\n\tValore: %s\n", length, str);
}

void receive_string(int fd, char str[]) {
    int length;
    read(fd, &length, sizeof(length));
    read(fd, str, length);

    printf("Ricevuta stringa:\n\tLunghezza: %d\n\tValore: %s\n", length, str);
}

struct sockaddr_in create_socket_address(const char *ip, int port) {
    struct sockaddr_in address;
    memset(&address, 0, sizeof(address));

    address.sin_family = AF_INET;
    if (ip) {
        struct hostent *host = gethostbyname(ip);
        address.sin_addr.s_addr =
            ((struct in_addr *)(host->h_addr_list[0]))->s_addr;
    } else {
        address.sin_addr.s_addr = INADDR_ANY;
    }

    address.sin_port = htons(port);

    return address;
}

int main(int argc, char **argv) {
    if (argc != 3) {
        printf("Usage: client <server_ip> <server_port>\n");
    }

    struct sockaddr_in server_address =
        create_socket_address(argv[1], atoi(argv[2]));

    char line[256];
    char buf[256];

    int sock = socket(AF_INET, SOCK_STREAM, 0);
    if (sock < 0) {
        return 1;
    }

    if (connect(sock, (struct sockaddr *)&server_address,
                sizeof(server_address)) < 0) {
        close(sock);
        return 1;
    }

    printf("Specificare l'operazione da svolgere: ");
    while (gets(line)) {
        if (strcmp(line, "mget") == 0) {
            send_string(sock, line);

            printf("Specificare la cartella da ricevere: ");
            if (!gets(line)) {
                break;
            }

            send_string(sock, line);

            char dir_available;
            read(sock, &buf, sizeof(char));
            if (!buf[0]) {
                printf("Cartella non disponibile sul server\n");
                continue;
            }

            printf("Cartella presente sul server\n");

            receive_string(sock, buf);
            while (strcmp(buf, "[fine]") != 0) {

                long file_size;
                read(sock, &file_size, sizeof(file_size));

                printf("Ricezione file: %s, dimensione: %d\n", buf, file_size);

                int file = open(buf, O_WRONLY | O_CREAT, 0777);
                if (file < 0) {
                    printf("Errore creazione file: %s\n", buf);
                }
                long n_read = 0;
                while (n_read < file_size) {
                    int current_read = read(sock, buf, sizeof(buf));

                    write(file, buf, current_read);

                    if (!current_read) {
                        printf("Connessione chiusa in mezzo alla trasmissione "
                               "file\n");
                        break;
                    }
                    n_read += current_read;
                }
                close(file);
                send_string(sock, "[continue]");

                receive_string(sock, buf);
            }
        } else if (strcmp(line, "mput") == 0) {
            send_string(sock, line);

            DIR *dir = 0;
            char folder_name[256];
            while (!dir) {
                printf("Specificare la cartella da inviare: ");

                gets(folder_name);
                dir = opendir(folder_name);
            }

            int folder_name_length = strlen(folder_name);
            folder_name[folder_name_length++] = '/';
            folder_name[folder_name_length + 1] = 0;

            struct dirent *entry;
            while ((entry = readdir(dir)) != 0) {
                char file_name[256];
                strcpy(file_name, folder_name);
                strcat(file_name, entry->d_name);

                int file = open(file_name, O_RDONLY);
                if (file < 0) {
                    continue;
                }

                long int file_length = lseek(file, 0, SEEK_END);
                lseek(file, 0, SEEK_SET);
                if ((int)file_length <= 0) {
                    continue;
                }

                send_string(sock, entry->d_name);
                write(sock, &file_length, sizeof(file_length));

                int n_read;
                char buf[256];
                while ((n_read = read(file, buf, sizeof(buf)))) {
                    write(sock, buf, n_read);
                }

                receive_string(sock, buf);
                if (strcmp(buf, "[continue]") != 0) {
                    break;
                }
            }
        }

        printf("Specificare l'operazione da svolgere: ");
    }

    printf("Chiusura socket\n");
    shutdown(sock, SHUT_WR);
    close(sock);
}
