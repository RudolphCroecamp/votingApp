namespace votingBackend3.Models.DTOs
{
    public class ElectionResultsDTO
    {
        public Guid party_id { get; set; } = Guid.Empty;
        public string party_name { get; set; } = string.Empty;
        public int votes { get; set; } = 0;
        public double percentage { get; set; } = 0.0;
    }
}
