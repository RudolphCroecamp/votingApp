using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace votingBackend3.Migrations
{
    /// <inheritdoc />
    public partial class UpdateAuthTable : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.RenameColumn(
                name: "is_active",
                table: "auth",
                newName: "allowed");

            migrationBuilder.AddColumn<string>(
                name: "password",
                table: "auth",
                type: "text",
                nullable: false,
                defaultValue: "");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "password",
                table: "auth");

            migrationBuilder.RenameColumn(
                name: "allowed",
                table: "auth",
                newName: "is_active");
        }
    }
}
