namespace votingBackend3.Models.DTOs
{
    public class BlockData
    {
        public Guid vote_id { get; set; } = Guid.Empty;
        public Guid token_id { get; set; } = Guid.Empty;
        public Guid election_id { get; set; } = Guid.Empty;
        public Guid party_id { get; set; } = Guid.Empty;
    } 
}
