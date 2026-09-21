using System.ComponentModel.DataAnnotations;
using System.Diagnostics.CodeAnalysis;
using votingBackend3.Models.Entities;

namespace votingBackend3.Models.DTOs
{
    public class VoteToken
    {
        public Guid token_id { get; set; } = Guid.NewGuid(); // Unique identifier for the voting token
        public string Token { get; set; } = string.Empty; //token to authenticate the vote
        public DateTime IssuedAt { get; set; } = DateTime.UtcNow; // Timestamp when the token was issued
    }
}
