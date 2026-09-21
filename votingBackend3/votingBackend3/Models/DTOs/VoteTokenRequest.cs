using System.ComponentModel.DataAnnotations;
using votingBackend3.Models.Entities;

namespace votingBackend3.Models.DTOs
{
    public class VoteTokenRequest
    {
        public Guid election_id { get; set; } = Guid.Empty;
    }
}
