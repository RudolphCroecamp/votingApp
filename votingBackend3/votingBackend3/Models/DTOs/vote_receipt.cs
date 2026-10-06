namespace votingBackend3.Models.DTOs
{
    public class vote_receipt
    {
        public Guid election_id { get; set; } = Guid.Empty;
        public string election_name { get; set; } = string.Empty;
        public string election_type { get; set; } = string.Empty;
        public bool hasVoted { get; set; } = false;
        public DateTime voted_at { get; set; }
    }
}
