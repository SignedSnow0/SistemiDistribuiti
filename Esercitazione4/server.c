#include <dirent.h>
#include <errno.h>
#include <fcntl.h>
#include <memory.h>
#include <netdb.h>
#include <netinet/in.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/select.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <unistd.h>

#define BUFFER_LENGTH 256
#define MAX_TCP_QUEUE 5

typedef struct {
    char filename[BUFFER_LENGTH];
    char parola[BUFFER_LENGTH];
} Datagram;

struct sockaddr_in create_socket_addres(const char *ip, const int port) {
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

int parse_args(const char *str, char *command, char *args[BUFFER_LENGTH]) {
    int n_args = 0;

    memcpy(command, str, strlen(str) + 1);
    args[n_args++] = str;

    int i = 0;
    while (command[i]) {
        if (command[i] == ' ') {
            command[i] = 0;

            args[n_args++] = &(command[i + 1]);
        }

        i++;
    }
    args[n_args] = 0;

    return n_args;
}

int main(int argc, char **argv) {
    if (argc != 3) {
        printf("Utilizzo: server <portaUDP> <portaTCP>\n");
        return 1;
    }

    struct sockaddr_in udp_sock_address =
        create_socket_addres(0, atoi(argv[1]));
    struct sockaddr_in tcp_sock_address =
        create_socket_addres(0, atoi(argv[2]));

    int tcp_sock = socket(AF_INET, SOCK_STREAM, 0);
    if (tcp_sock < 0) {
        printf("Errore creazione socket tcp!\n");
        return 1;
    }

    int udp_sock = socket(AF_INET, SOCK_DGRAM, 0);
    if (udp_sock < 0) {
        printf("Errore creazione socket udp!\n");
        return 1;
    }

    int enable = 1;
    if (setsockopt(tcp_sock, SOL_SOCKET, SO_REUSEADDR, &enable,
                   sizeof(enable)) < 0) {
        return 1;
    }

    if (bind(tcp_sock, (struct sockaddr *)&tcp_sock_address,
             sizeof(tcp_sock_address)) < 0) {
        printf("Errore bind socket tcp!\n");
        return 1;
    }

    if (listen(tcp_sock, MAX_TCP_QUEUE) < 0) {
        printf("Errore listen!\n");
        return 1;
    }

    if (bind(udp_sock, (struct sockaddr *)&udp_sock_address,
             sizeof(udp_sock_address)) < 0) {
        printf("Errore bind socket udp!\n");
        return 1;
    }

    fd_set read_mask;
    FD_ZERO(&read_mask);
    FD_SET(tcp_sock, &read_mask);
    FD_SET(udp_sock, &read_mask);

    while (select(udp_sock + 1, &read_mask, 0, 0, 0) > 0) {
        if (FD_ISSET(udp_sock, &read_mask)) {
            char buf[BUFFER_LENGTH];
            struct sockaddr_in dst_address;
            unsigned int dst_address_length;

            recvfrom(udp_sock, buf, sizeof(buf), 0,
                     (struct sockaddr *)&dst_address, &dst_address_length);

            char *args[BUFFER_LENGTH];
            int n_args = parse_args(buf, buf, args);

            printf("Ricevuto comando udp:\n");
            for (int i = 0; i < n_args; i++) {
                printf("\tArgomento:  %s\n", args[i]);
            }

            int exec_pid = fork();
            if (exec_pid == 0) {
                execvp(buf, args);

                printf("Errore exec\n");
                exit(1);
            } else if (exec_pid > 0) {
                int status;
                waitpid(exec_pid, &status, 0);

                int exit = -1;
                if (WIFEXITED(status)) {
                    exit = WEXITSTATUS(status);
                }
                printf("Risultato exec: %d\n", exit);

                sendto(udp_sock, &exit, sizeof(exit), 0,
                       (struct sockaddr *)&dst_address, dst_address_length);
            }
        } else if (FD_ISSET(tcp_sock, &read_mask)) {
            int sock;
            struct sockaddr_in sock_address;
            unsigned int sock_address_length;

            if ((sock = accept(tcp_sock, (struct sockaddr *)&sock_address,
                               &sock_address_length)) < 0) {
                if (errno == EINTR) {
                    printf("Errore interrupt\n");
                    continue;
                }

                printf("Errore accept\n");

                return 1;
            }

            printf("Connessione accettata\n");

            if (fork() == 0) {
                close(tcp_sock);

                char buf[BUFFER_LENGTH];

                read(sock, buf, sizeof(buf));
                while (buf[0]) {
                    char *args[BUFFER_LENGTH];
                    int n_args = parse_args(buf, buf, args);

                    printf("Ricevuto comando tcp:\n");
                    for (int i = 0; i < n_args; i++) {
                        printf("\tArgomento:  %s\n", args[i]);
                    }

                    int exec_pid = fork();
                    if (exec_pid == 0) {
                        close(1);
                        dup(sock);
                        close(sock);

                        execvp(buf, args);

                        printf("Errore exec\n");
                        exit(1);
                    } else if (exec_pid > 0) {
                        int status;
                        waitpid(exec_pid, &status, 0);

                        char eof = 0;
                        write(sock, &eof, sizeof(eof));
                    } else {
                        close(sock);
                    }

                    memset(buf, 0, sizeof(buf));
                    read(sock, buf, sizeof(buf));
                }

                close(sock);
                printf("Connessione chiusa\n");
            }
        }
    }
}
