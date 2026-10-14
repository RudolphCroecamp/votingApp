namespace votingBackend3.Models.Entities
{
    public enum ElectionVoterStatus
    {
        Pending = 0,
        Approved = 1,
        Rejected = 2
    }

    public class ElectionVoter
    {
        public Guid election_id { get; set; }
        public Guid voter_id { get; set; }

        public ElectionVoterStatus status { get; set; } = ElectionVoterStatus.Pending;

        public bool has_voted { get; set; } = false;

        public Election Election { get; set; } = null!;
        public Voter Voter { get; set; } = null!;
    }
}

