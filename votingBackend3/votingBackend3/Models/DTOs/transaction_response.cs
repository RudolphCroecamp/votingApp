namespace votingBackend3.Models.DTOs
{
    public class transaction_response
    {
        public List<string> messages { get; set; } = new List<string>();
        public List<bool> status { get; set; } = new List<bool>();
    }
}
