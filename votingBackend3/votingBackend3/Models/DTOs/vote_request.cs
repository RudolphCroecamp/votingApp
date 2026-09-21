namespace votingBackend3.Models.DTOs
{
    public class vote_request
    {
        public VoteToken voteToken { get; set; } = null;//token to authenticate the vote -> 982bcc58-dd58-484e-b51f-80adc43accb7
        public Guid election_id { get; set; } = Guid.Empty;
        public Guid party_id { get; set; } = Guid.Empty;
        public string election_type { get; set; } = string.Empty;
    }
}
