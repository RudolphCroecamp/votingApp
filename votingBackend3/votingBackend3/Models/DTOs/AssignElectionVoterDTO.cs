using System.ComponentModel.DataAnnotations;
using votingBackend3.Models.Entities;

namespace votingBackend3.Models.DTOs
{
    public class AssignElectionVoterDTO
    {
        [Required]
        public Guid election_id { get; set; } = Guid.Empty;
        [Required]
        public ElectionVoterStatus status { get; set; } = ElectionVoterStatus.Pending;
        [Required]
        public Guid voter_id { get; set; } = Guid.Empty;
    }
}
