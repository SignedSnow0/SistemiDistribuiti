struct Endpoint {
    char hostname[256];
    int port;
};

struct Input {
    char directory[256];
    int server_active;
    Endpoint client;
};

struct File {
    char name[256];
};

struct Output {
    Endpoint server;
    File files[8];
    int num_files;
};

program MGET_PROG {
    version MGET_VERS {
        Output LIST_FILES(Input) = 1;
    } = 1;
} = 0x20000015;
