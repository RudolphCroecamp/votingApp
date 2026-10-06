using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace votingBackend3.Migrations
{
    /// <inheritdoc />
    public partial class adddocumentdb5 : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "fileSize",
                table: "DocumentInfo");

            migrationBuilder.RenameColumn(
                name: "originalFileName",
                table: "DocumentInfo",
                newName: "FileName");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.RenameColumn(
                name: "FileName",
                table: "DocumentInfo",
                newName: "originalFileName");

            migrationBuilder.AddColumn<int>(
                name: "fileSize",
                table: "DocumentInfo",
                type: "integer",
                nullable: false,
                defaultValue: 0);
        }
    }
}
