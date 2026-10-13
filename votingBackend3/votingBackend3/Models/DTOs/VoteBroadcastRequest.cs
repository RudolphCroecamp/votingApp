using System.ComponentModel.DataAnnotations;

namespace votingBackend3.Models.DTOs
{
    public class VoteBroadcastRequest
    {
        [Required]
        public Guid vote_id { get; set; } = Guid.Empty;
        [Required]
        public Guid token_id { get; set; } = Guid.Empty;
        [Required]
        public Guid election_id { get; set; } = Guid.Empty;
        [Required]
        public Guid party_id { get; set; } = Guid.Empty;
        [Required]
        public DateTime created_at { get; set; }
    }
}
