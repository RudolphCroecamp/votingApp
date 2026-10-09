namespace votingBackend3.Models.Entities
{
    public class ElectionVoter
    {
        public Guid election_id { get; set; }
        public Guid voter_id { get; set; }

        public bool has_voted { get; set; } = false;

        public Election Election { get; set; } = null!;
        public Voter Voter { get; set; } = null!;
    }
}

