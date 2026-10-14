using votingBackend3.Models.Entities;

namespace votingBackend3.Models.DTOs
{
    public class VoterListDTO
    {
        public List<VoterDTO> voters { get; set; }
        public int voterCount { get; set; } = 0;
    }
}
