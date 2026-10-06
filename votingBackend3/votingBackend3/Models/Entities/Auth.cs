using System.ComponentModel.DataAnnotations;

namespace votingBackend3.Models.Entities
{
    public class Auth
    {
        [Key]
        public Guid voter_id { get; set; } = Guid.Empty;
        [Required]
        public string id_number { get; set; } = string.Empty;
        [Required]
        public double[] face_embedding { get; set; } = Array.Empty<double>();
        [Required]
        public bool is_active { get; set; } = false;
        [Required]
        public string role { get; set; } = string.Empty;
        [Required]
        public DateTime created_at { get; set; }
    }
}
