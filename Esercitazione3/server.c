#include <dirent.h>
#include <errno.h>
#include <fcntl.h>
#include <memory.h>
#include <netdb.h>
#include <netinet/in.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <unistd.h>

void send_string(int fd, const char *str) {
    int length = 0;
    while (str[length++])
        ;

    write(fd, &length, sizeof(length));
    write(fd, str, length);

    printf("Inviata stringa:\n\tLunghezza: %d\n\tValore: %s\n", length, str);
}

int receive_string(int fd, char str[]) {
    int length = 0;
    read(fd, &length, sizeof(length));
    read(fd, str, length);

    printf("Ricevuta stringa:\n\tLunghezza: %d\n\tValore: %s\n", length, str);

    return length;
}

int main(int argc, char **argv) {
    if (argc != 2) {
        printf("Utilizzo: server <porta>\n");
        return 1;
    }

    struct sockaddr_in listen_address;
    memset(&listen_address, 0, sizeof(listen_address));

    listen_address.sin_family = AF_INET;
    listen_address.sin_addr.s_addr = INADDR_ANY;
    listen_address.sin_port = htons(atoi(argv[1]));

    int listen_sock;
    if ((listen_sock = socket(AF_INET, SOCK_STREAM, 0)) < 0) {
        printf("Errore creazione socket genitore\n");
        return 1;
    }

    const int on = 1;
    if (setsockopt(listen_sock, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) <
        0) {
        perror("set opzioni socket d'ascolto");
        return 1;
    }

    if (bind(listen_sock, (struct sockaddr *)&listen_address,
             sizeof(listen_address)) < 0) {
        printf("Errore bind socket\n");
        return 1;
    }

    if (listen(listen_sock, 5) < 0) {
        printf("Errore listen\n");
        return 1;
    }

    while (1) {
        struct sockaddr_in sock_addr;
        unsigned int sock_addr_length;
        int sock;
        if ((sock = accept(listen_sock, (struct sockaddr *)&sock_addr,
                           &sock_addr_length)) < 0) {
            if (errno == EINTR) {
                continue;
            }

            printf("Errore accept\n");
            return 1;
        }

        if (fork() == 0) {
            close(listen_sock);

            char buf[256];
            while (receive_string(sock, buf)) {
                if (strcmp(buf, "mget") == 0) {
                    char folder_name[256];
                    receive_string(sock, folder_name);

                    DIR *dir = opendir(folder_name);
                    int folder_length = strlen(folder_name);
                    folder_name[folder_length++] = '/';
                    folder_name[folder_length + 1] = 0;

                    if (dir) {
                        buf[0] = 1;
                        write(sock, buf, sizeof(char));
                    } else {
                        buf[0] = 0;
                        write(sock, buf, sizeof(char));
                        break;
                    }

                    struct dirent *entry;
                    while ((entry = readdir(dir)) != 0) {
                        char file_name[256];
                        strcpy(file_name, folder_name);
                        strcat(file_name, entry->d_name);

                        if (entry->d_type == DT_DIR) {
                            printf("Dir\n");
                        }

                        int file = open(file_name, O_RDONLY);
                        if (file < 0) {
                            printf("Errore apertura file\n");
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
                        while ((n_read = read(file, buf, sizeof(buf)))) {
                            write(sock, buf, n_read);
                        }

                        receive_string(sock, buf);
                        if (strcmp(buf, "continue") != 0) {
                            break;
                        }
                    }

                    send_string(sock, "[fine]");
                } else if (strcmp(buf, "mput") == 0) {
                }
            }

            printf("Fine processo\n");
            close(sock);
        }
    }
}
