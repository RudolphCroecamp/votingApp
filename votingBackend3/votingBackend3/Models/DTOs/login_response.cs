namespace votingBackend3.Models.DTOs
{
    public class login_response
    {
        public string Token { get; set; } = string.Empty;

        public DateTime ExpiresAt { get; set; }
    }
}
