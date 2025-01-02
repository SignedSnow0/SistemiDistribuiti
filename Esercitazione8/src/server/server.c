#include "proto.h"

#define VECTOR_GROW_FACTOR 2

struct Candidate {
    char name[STRING_LENGTH];
    char judge[STRING_LENGTH];
    char category;
    char file[STRING_LENGTH];
    char phase;
    int vote;
};

struct Vector {
    struct Candidate *data;
    int length;
    int size;
};

struct Vector vector_new() {
    struct Vector vector;
    vector.length = 0;
    vector.size = 1;
    vector.data = malloc(sizeof(struct Candidate));

    return vector;
}

void vector_free(struct Vector *vector) { free(vector->data); }

void vector_push(struct Vector *vector, struct Candidate item) {
    if (vector->length + 1 > vector->size) {
        struct Candidate *old = vector->data;

        vector->data = malloc(sizeof(struct Candidate) * vector->size *
                              VECTOR_GROW_FACTOR);
        memcpy(vector->data, old, sizeof(struct Candidate) * vector->size);
        free(old);

        vector->size *= VECTOR_GROW_FACTOR;
    }

    vector->data[vector->length] = item;
    vector->length++;
}

void vector_pop(struct Vector *vector) {
    vector->length--;
    if (vector->length < vector->size / VECTOR_GROW_FACTOR) {
        vector->size /= VECTOR_GROW_FACTOR;
        struct Candidate *old = vector->data;

        vector->data = malloc(sizeof(struct Candidate) * vector->size);
        memcpy(vector->data, old, sizeof(struct Candidate) * vector->size);
        free(old);
    }
}

void vector_remove(struct Vector *vector, int index) {
    if (index >= vector->length) {
        return;
    }

    if (index + 1 == vector->length) {
        vector_pop(vector);
        return;
    }

    int tmp_length = vector->length - (index + 1);
    struct Candidate *tmp = malloc(sizeof(struct Candidate) * tmp_length);
    memcpy(tmp, &vector->data[index + 1],
           sizeof(struct Candidate) * tmp_length);

    memcpy(&vector->data[index], tmp, sizeof(struct Candidate) * tmp_length);
    free(tmp);

    vector->length--;
    if (vector->length < vector->size / VECTOR_GROW_FACTOR) {
        vector->size /= VECTOR_GROW_FACTOR;
        struct Candidate *old = vector->data;

        vector->data = malloc(sizeof(struct Candidate) * vector->size);
        memcpy(vector->data, old, sizeof(struct Candidate) * vector->size);
        free(old);
    }
}

static struct Vector candidates;
static int initialized = 0;

void initialize() {
    if (initialized) {
        return;
    }

    candidates = vector_new();

    struct Candidate candidate;
    strcpy(candidate.name, "Alberto");
    strcpy(candidate.judge, "Giud.1");
    candidate.category = 'U';
    strcpy(candidate.file, "L");
    candidate.phase = 'A';
    candidate.vote = 0;

    vector_push(&candidates, candidate);

    strcpy(candidate.name, "Bianca");
    strcpy(candidate.judge, "Giud.2");
    candidate.category = 'D';
    candidate.phase = 'B';
    candidate.vote = 0;

    vector_push(&candidates, candidate);

    strcpy(candidate.name, "Carlo");
    strcpy(candidate.judge, "Giud.1");
    candidate.category = 'B';
    candidate.phase = 'S';
    candidate.vote = 0;

    vector_push(&candidates, candidate);

    strcpy(candidate.name, "Davide");
    strcpy(candidate.judge, "Giud.2");
    candidate.category = 'U';
    candidate.phase = 'A';
    candidate.vote = 0;

    vector_push(&candidates, candidate);

    strcpy(candidate.name, "Elena");
    strcpy(candidate.judge, "Giud.3");
    candidate.category = 'D';
    candidate.phase = 'B';
    candidate.vote = 0;

    vector_push(&candidates, candidate);

    initialized = 1;
}

Output *judge_leaderboard_1_svc(void *input, struct svc_req *request) {
    initialize();

    static Output output;
    memset(&output, 0, sizeof(Output));

    int set_judges = 0, i = 0;
    while (i < candidates.length && set_judges < NUM_JUDGES) {
        int found = 0;
        for (int j = 0; j < set_judges; j++) {
            if (strcmp(candidates.data[i].judge,
                       output.judgeLeaderboard[j].name) == 0) {
                found = 1;
                output.judgeLeaderboard[j].score += candidates.data[i].vote;
                break;
            }
        }

        if (!found) {
            strcpy(output.judgeLeaderboard[set_judges].name,
                   candidates.data[i].judge);
            output.judgeLeaderboard[set_judges].score = candidates.data[i].vote;

            set_judges++;
        }

        i++;
    }

    for (i = 0; i < set_judges; i++) {
        for (int j = i + 1; j < set_judges; j++) {
            if (output.judgeLeaderboard[i].score <
                output.judgeLeaderboard[j].score) {
                int tmp = output.judgeLeaderboard[i].score;
                output.judgeLeaderboard[i].score =
                    output.judgeLeaderboard[j].score;
                output.judgeLeaderboard[j].score = tmp;

                char tmp_judge[STRING_LENGTH];
                strcpy(tmp_judge, output.judgeLeaderboard[i].name);
                strcpy(output.judgeLeaderboard[i].name,
                       output.judgeLeaderboard[j].name);
                strcpy(output.judgeLeaderboard[j].name, tmp_judge);
            }
        }
    }

    return &output;
}

int *add_vote_1_svc(Input *input, struct svc_req *request) {
    initialize();

    static int result;

    for (int i = 0; i < candidates.length; i++) {
        if (strcmp(candidates.data[i].name, input->candidateName) == 0) {
            candidates.data[i].vote++;
            result = 1;
            return &result;
        }
    }

    result = 0;
    return &result;
}

int *remove_vote_1_svc(Input *input, struct svc_req *request) {
    initialize();

    static int result;

    for (int i = 0; i < candidates.length; i++) {
        if (strcmp(candidates.data[i].name, input->candidateName) == 0) {
            candidates.data[i].vote--;
            result = 1;
            return &result;
        }
    }

    result = 0;
    return &result;
}

int *insert_candidate_1_svc(Input *input, struct svc_req *request) {
    initialize();

    static int result;

    for (int i = 0; i < candidates.length; i++) {
        if (strcmp(candidates.data[i].name, input->candidateName) == 0) {
            result = 0;
            return &result;
        }
    }

    struct Candidate candidate;
    strcpy(candidate.name, input->candidateName);
    strcpy(candidate.judge, input->judgeName);
    candidate.category = input->category;
    strcpy(candidate.file, "L");
    candidate.phase = input->phase;
    candidate.vote = 0;

    vector_push(&candidates, candidate);

    result = 1;
    return &result;
}

int *remove_candidate_1_svc(Input *input, struct svc_req *request) {
    initialize();

    static int result;

    for (int i = 0; i < candidates.length; i++) {
        if (strcmp(candidates.data[i].name, input->candidateName) == 0) {
            vector_remove(&candidates, i);
            result = 1;
            return &result;
        }
    }

    result = 0;
    return &result;
}
