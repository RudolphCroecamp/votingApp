namespace votingBackend3.Models.Entities
{
    public class ElectionVoter
    {
        public Guid election_id { get; set; }
        public Election Election { get; set; } = null!;

        public Guid voter_id { get; set; }
        public Voter Voter { get; set; } = null!;

        public Vote? Vote { get; set; }
    }
}
