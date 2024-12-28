const NUM_JUDGES = 12;
const STRING_LENGTH = 256;

struct Judge {
	char name[STRING_LENGTH]; 
	int score;
}; 

struct Output {
	Judge judgeLeaderboard[NUM_JUDGES]; 
};

struct Input {
	char candidateName[STRING_LENGTH];
	char judgeName[STRING_LENGTH];
	char category;
	char phase;
};
  
program XFACTOR {
	version XFACTOR_VERS {
		Output JUDGE_LEADERBOARD(void) = 1;        
        int ADD_VOTE(Input) = 2;
		int REMOVE_VOTE(Input) = 3;
		int INSERT_CANDIDATE(Input) = 4;
		int REMOVE_CANDIDATE(Input) = 5;
	} = 1;
} = 0x20000013;