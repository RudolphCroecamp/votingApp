using System.ComponentModel.DataAnnotations;

namespace votingBackend3.Models.Entities
{
    public class Auth
    {
        [Key]
        public Guid voter_id { get; set; }

        public string id_number { get; set; } = string.Empty;

        public double[] face_embedding { get; set; } = Array.Empty<double>();

        public bool is_active { get; set; }

        public string role { get; set; } = string.Empty;

        public DateTime created_at { get; set; }
    }
}
