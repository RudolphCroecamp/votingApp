namespace votingBackend3.Models.DTOs
{
    public class VoterProfileDto
    {
        public string id_number { get; set; } = string.Empty;
        public string firstname { get; set; } = string.Empty;
        public string lastname { get; set; } = string.Empty;
        public string province { get; set; } = string.Empty;
        public string city { get; set; } = string.Empty;
        public string street { get; set; } = string.Empty;
        public int postal { get; set; } = 0;
        public string phone { get; set; } = string.Empty;
    }
}
