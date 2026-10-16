using System.ComponentModel.DataAnnotations;

namespace votingBackend3.Models.DTOs
{
    public class RequestElectionAccessDto
    {
        [Required]
        public Guid election_id { get; set; } = Guid.Empty;
    }
}
