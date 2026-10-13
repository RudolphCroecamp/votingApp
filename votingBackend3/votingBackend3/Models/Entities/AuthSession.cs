using System.ComponentModel.DataAnnotations;

namespace votingBackend3.Models.Entities
{
    public class AuthSession
    {
        [Key]
        public Guid jti { get; set; }

        [Required]
        public Guid voter_id { get; set; }

        [Required]
        public DateTime created_at { get; set; }

        [Required]
        public DateTime expires_at { get; set; }

        public DateTime? revoked_at { get; set; }

        public Auth? Auth { get; set; }
    }
}
