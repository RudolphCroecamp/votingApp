using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace votingBackend3.Migrations
{
    /// <inheritdoc />
    public partial class adddocumentdb3 : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddForeignKey(
                name: "FK_DocumentInfo_voter_fileId",
                table: "DocumentInfo",
                column: "fileId",
                principalTable: "voter",
                principalColumn: "voter_id",
                onDelete: ReferentialAction.Cascade);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "FK_DocumentInfo_voter_fileId",
                table: "DocumentInfo");
        }
    }
}
