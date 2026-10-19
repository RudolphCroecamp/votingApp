using System.ComponentModel.DataAnnotations;

namespace votingBackend3.Models.DTOs
{
    public class register_request
    {
        [Required]
        public string id_number { get; set; } = string.Empty;
        [Required]
        public double[] face_embedding { get; set; } = Array.Empty<double>();
        [Required]
        public string firstname { get; set; } = string.Empty;
        [Required]
        public string lastname { get; set; } = string.Empty;
        [Required]
        public string province { get; set; } = string.Empty;
        [Required]
        public string city { get; set; } = string.Empty;
        [Required]
        public string street { get; set; } = string.Empty;
        [Required]
        public int postal { get; set; }
        // Optional so older app builds that don't send it can still register.
        public string? phone { get; set; }
        [Required]
        public IFormFile id_front { get; set; } = null!;
        [Required]
        public IFormFile id_back { get; set; } = null!;

    }
}