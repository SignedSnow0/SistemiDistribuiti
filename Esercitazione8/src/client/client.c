#include "proto.h"

int main(int argc, char** argv) {
    if (argc != 2) {
        printf("Usage: %s <server>\n", argv[0]);
        return 1;
    }

    CLIENT* client;
    if (!(client = clnt_create(argv[1], XFACTOR, XFACTOR_VERS, "tcp"))) {
        printf("Error: could not connect to server\n");
        return 1;
    }

    Input input;
    printf(
        "Select your action (L=leaderboard, V=vote, R=remove vote, A=Add "
        "candidate, r=remove candidate): ");
    char buf[STRING_LENGTH];
    while (gets(buf)) {
        if (buf[0] == 'L') {
            Output* output = judge_leaderboard_1(0, client);

            for (int i = 0; i < NUM_JUDGES; i++) {
                if (output->judgeLeaderboard[i].name[0] == 0) {
                    break;
                }

                printf("%s: %d\n", output->judgeLeaderboard[i].name,
                       output->judgeLeaderboard[i].score);
            }
        } else if (buf[0] == 'V') {
            printf("Contestant name: ");
            gets(input.candidateName);

            int* result = add_vote_1(&input, client);
        } else if (buf[0] == 'R') {
            printf("Contestant name: ");
            gets(input.candidateName);

            int* result = remove_vote_1(&input, client);
        } else if (buf[0] == 'A') {
            printf("Contestant name: ");
            gets(input.candidateName);

            printf("Judge name: ");
            gets(input.judgeName);

            printf("Category: ");
            gets(buf);
            input.category = buf[0];

            printf("Phase: ");
            gets(buf);
            input.phase = buf[0];

            int* result = insert_candidate_1(&input, client);
        } else if (buf[0] == 'r') {
            printf("Contestant name: ");
            gets(input.candidateName);

            int* result = remove_candidate_1(&input, client);
        } else {
            printf("Invalid action\n");
        }

        printf(
            "Select your action (L=leaderboard, V=vote, R=remove vote, A=Add "
            "candidate, r=remove candidate): ");
    }
}
