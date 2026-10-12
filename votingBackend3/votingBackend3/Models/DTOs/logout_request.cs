namespace votingBackend3.Models.DTOs
{
    public class logout_request
    {
        public Guid voter_id { get; set; }

        public string token { get; set; } = string.Empty;
    }
}