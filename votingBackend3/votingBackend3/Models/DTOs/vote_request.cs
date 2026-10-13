using System.ComponentModel.DataAnnotations;
using System.Text.Json.Serialization;

namespace votingBackend3.Models.DTOs
{
    public class vote_request
    {
        [Required]
        public VoteTokenDto voteToken { get; set; } = null;//token to authenticate the vote -> 982bcc58-dd58-484e-b51f-80adc43accb7
        [Required]
        public Guid election_id { get; set; } = Guid.Empty;
        [Required]
        public Guid party_id { get; set; } = Guid.Empty;
    }
}
