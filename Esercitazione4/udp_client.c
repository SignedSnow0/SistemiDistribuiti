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

    // Inizializzazione server address e socket connessa
    struct hostent *host = gethostbyname(argv[1]);
    struct sockaddr_in address;
    memset(&address, 0, sizeof(address));

    address.sin_family = AF_INET;
    address.sin_addr.s_addr =
        ((struct in_addr *)(host->h_addr_list[0]))->s_addr;
    address.sin_port = htons(atoi(argv[2]));

    int sock = socket(AF_INET, SOCK_DGRAM, 0);
    if (sock < 0) {
        printf("Errore creazione socket!\n");
        return 1;
    }

    char buf[BUFFER_LENGTH];
    printf("Inserire comando da eseguire: ");
    while (gets(buf)) {
        // Invio comando da input a socket
        sendto(sock, buf, strlen(buf), 0, (struct sockaddr *)&address,
               sizeof(address));

        // lettura codice restituito dal server
        int exit_value;
        struct sockaddr_in server_address;
        socklen_t server_address_length;
        recvfrom(sock, &exit_value, sizeof(exit_value), 0,
                 (struct sockaddr *)&server_address, &server_address_length);

        printf("Comand eseguito con valore di uscita: %d\n", exit_value);
        printf("Inserire comando da eseguire: ");
    }

    // Indicazione termine ciclo al server
    buf[0] = 0;
    write(sock, buf, sizeof(char));

    close(sock);
}
