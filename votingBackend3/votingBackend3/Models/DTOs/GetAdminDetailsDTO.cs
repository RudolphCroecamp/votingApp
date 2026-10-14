using System.ComponentModel.DataAnnotations;

namespace votingBackend3.Models.DTOs
{
    public class GetAdminDetailsDTO
    {
        [Required]
        public Guid user_id { get; set; } = Guid.Empty;
        [Required]
        public string firstname { get; set; } = string.Empty;
        [Required]
        public string lastname { get; set; } = string.Empty;
        [Required]
        public string province { get; set; } = string.Empty;
        [Required]
        public string city { get; set; } = string.Empty;
        [Required, DataType(DataType.PostalCode)]
        public int postal { get; set; } = 0;
        [Required]
        public string role { get; set; } = string.Empty;
        [Required]
        public bool allowed { get; set; } = false;
        [Required]
        public DateTime created_at { get; set; }
    }
}
