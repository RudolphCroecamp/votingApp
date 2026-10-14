using votingBackend3.Models.Entities;

namespace votingBackend3.Models.DTOs
{
    public class ElectionVoterDto
    {
        public Guid election_id { get; set; } = Guid.Empty;
        public Guid voter_id { get; set; } = Guid.Empty;
        public ElectionVoterStatus status { get; set; } = ElectionVoterStatus.Pending;
        public bool voted { get; set; } = false;
    }
}
