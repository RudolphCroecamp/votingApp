using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using System.Reflection.Metadata;
using votingBackend3.Data;
using votingBackend3.Models.Entities;
using votingBackend3.Services;

namespace votingBackend3.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class DocumentController : ControllerBase
    {
        private readonly DocumentService _DocumentService;

        public DocumentController(DocumentService documentService)
        {
            _DocumentService = documentService;
        }


        [HttpPost]
        [Route("uploadFiles")]
        public async Task<IActionResult> UploadFiles(List<IFormFile> files)
        {
            try
            {
                if (files == null || files.Count == 0) return BadRequest("No file was uploaded.");

                List<DocumentInfo> documents = await _DocumentService.uploadFiles(files);

                return Ok(new
                {
                    message = "Document uploaded successfully",
                    documents = documents
                });
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);
                return BadRequest("Failed to upload document. Try again later.");
            }


        }


        [HttpGet]
        [Route("getFiles/{voter_id}")]
        public async Task<IActionResult> GetFile(Guid voter_id)
        {
            try
            {
                var files = await _DocumentService.GetFiles(voter_id);

                if (files == null) return NotFound("Files not found");


                return Ok(new
                {
                    message = "The following id photos are related to this user",
                    documents = files
                });

            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);
                return BadRequest("Request failed");
            }

        }

    }
}

