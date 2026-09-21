namespace votingBackend3.Models.Entities
{
    public class ElectionParty
    {
        public Guid election_id { get; set; }
        public Election Election { get; set; } = null!;

        public Guid party_id { get; set; }
        public Party Party { get; set; } = null!;
    }
}
