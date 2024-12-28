include_dirs = /usr/include/tirpc
include_files = tirpc

server_dir = src/server
client_dir = src/client

rpcgen:
	cd ${server_dir}/; rpcgen proto.x
	cp ${server_dir}/proto_xdr.c ${client_dir}/proto_xdr.c
	cp ${server_dir}/proto.h ${client_dir}/proto.h
	mv ${server_dir}/proto_clnt.c ${client_dir}

server: rpcgen
	gcc -I ${include_dirs} ${server_dir}/*.c -o server -l${include_files}

client: rpcgen
	gcc -std=c99 -I ${include_dirs} ${client_dir}/*.c -o client -l${include_files}

all: server client
