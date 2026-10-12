namespace votingBackend3.Models.DTOs
{
    public class register_request
    {
        public string id_number { get; set; } = string.Empty;

        public string role { get; set; } = string.Empty;

        public double[] face_embedding { get; set; } = Array.Empty<double>();

        public string firstname { get; set; } = string.Empty;

        public string lastname { get; set; } = string.Empty;

        public string province { get; set; } = string.Empty;

        public string city { get; set; } = string.Empty;

        public string street { get; set; } = string.Empty;

        public int postal { get; set; }
    }
}