#include <dirent.h>
#include <fcntl.h>
#include <memory.h>
#include <netdb.h>
#include <netinet/in.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/select.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <unistd.h>

#define BUFFER_LENGTH 256

int main(int argc, char **argv) {
    if (argc != 3) {
        printf("Utilizzo: client <serverIP> <serverPort>\n");
    }

    struct sockaddr_in address;
    memset(&address, 0, sizeof(address));

    address.sin_family = AF_INET;

    struct hostent *host = gethostbyname(argv[1]);
    address.sin_addr.s_addr =
        ((struct in_addr *)(host->h_addr_list[0]))->s_addr;
    address.sin_port = htons(atoi(argv[2]));

    int sock = socket(AF_INET, SOCK_STREAM, 0);
    if (sock < 0) {
        printf("Errore creazione socket!\n");
        return 1;
    }

    if (connect(sock, (struct sockaddr *)&address, sizeof(address)) < 0) {
        printf("Errore connect!\n");
        return 1;
    }

    char buf[BUFFER_LENGTH];
    printf("Inserire comando da eseguire: ");
    while (gets(buf)) {
        write(sock, buf, strlen(buf));

        char c;
        while (read(sock, &c, sizeof(char)), c) {
            write(1, &c, sizeof(char));
        }

        printf("Inserire comando da eseguire: ");
    }

    buf[0] = 0;
    write(sock, buf, sizeof(char));
    close(sock);
}
