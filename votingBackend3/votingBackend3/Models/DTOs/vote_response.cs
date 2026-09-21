namespace votingBackend3.Models.DTOs
{
    public class vote_response
    {
        public Guid token_receipt { get; set; } = Guid.Empty;
        public string message { get; set; } = string.Empty;
    }
}
