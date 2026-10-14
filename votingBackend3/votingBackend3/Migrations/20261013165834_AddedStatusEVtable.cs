using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace votingBackend3.Migrations
{
    /// <inheritdoc />
    public partial class AddedStatusEVtable : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<int>(
                name: "status",
                table: "election_voter",
                type: "integer",
                nullable: false,
                defaultValue: 0);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "status",
                table: "election_voter");
        }
    }
}
