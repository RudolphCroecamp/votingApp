
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using System.Security.Claims;
using votingBackend3.Services;

namespace votingBackend3.Controllers
{
    [ApiController]
    [Route("api/documents")]
    public class DocumentsController : ControllerBase
    {
        private readonly DocumentService _documentService;

        public DocumentsController(DocumentService documentService)
        {
            _documentService = documentService;
        }

        // A voter can retrieve only their own document.
        [Authorize(Roles = "Voter")]
        [HttpGet("me/{fileId:guid}")]
        public async Task<IActionResult> GetMyDocument(Guid fileId)
        {
            var claim = User.FindFirstValue(ClaimTypes.NameIdentifier);

            if (!Guid.TryParse(claim, out var voterId)) return Unauthorized();

            var document = await _documentService.GetFile(voterId, fileId);

            if (document == null) return NotFound("No documents found for this user");

            return File(document.Content, document.ContentType);
        }

        // An admin can retrieve a document belonging to a voter.
        [Authorize(Roles = "Admin")]
        [HttpGet("voters/{voterId:guid}/{fileId:guid}")]
        public async Task<IActionResult> GetVoterDocument(Guid voterId, Guid fileId)
        {
            var document = await _documentService.GetFile(voterId, fileId);

            if (document == null) return NotFound("No documents found for this user");

            return File(document.Content, document.ContentType);
        }

        // An admin can retrieve a voter's document metadata.
        [Authorize(Roles = "Admin")]
        [HttpGet("voters/{voterId:guid}")]
        public async Task<IActionResult> GetVoterDocuments(Guid voterId)
        {
            var documents = await _documentService.GetFiles(voterId);

            return Ok(documents);
        }
    }
}
