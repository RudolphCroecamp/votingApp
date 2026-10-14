using System.ComponentModel.DataAnnotations;

namespace votingBackend3.Models.DTOs
{
    public class AssignPartyToElectionDto
    {
        [Required]
        public Guid party_id { get; set; } = Guid.Empty;
        [Required]
        public Guid election_id { get; set; } = Guid.Empty;
    }
}
