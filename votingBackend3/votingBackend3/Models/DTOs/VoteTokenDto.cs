
using Microsoft.EntityFrameworkCore.Metadata.Internal;
using System.ComponentModel.DataAnnotations;

namespace votingBackend3.Models.DTOs
{
    public class VoteTokenDto
    {
        [Key]
        public Guid token_id { get; set; } = Guid.NewGuid(); // Unique identifier for the voting token
        public string Token { get; set; } = string.Empty; //token to authenticate the vote
        public DateTime IssuedAt { get; set; } = DateTime.UtcNow; // Timestamp when the token was issued
    }
}
