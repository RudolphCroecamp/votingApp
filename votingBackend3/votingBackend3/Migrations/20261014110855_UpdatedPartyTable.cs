using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace votingBackend3.Migrations
{
    /// <inheritdoc />
    public partial class UpdatedPartyTable : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "party_description",
                table: "party");

            migrationBuilder.DropColumn(
                name: "party_name",
                table: "party");

            migrationBuilder.RenameColumn(
                name: "status",
                table: "party",
                newName: "allowed");

            migrationBuilder.AddColumn<string>(
                name: "abbreviation",
                table: "party",
                type: "character varying(20)",
                maxLength: 20,
                nullable: false,
                defaultValue: "");

            migrationBuilder.AddColumn<DateTime>(
                name: "created_at",
                table: "party",
                type: "timestamp with time zone",
                nullable: false,
                defaultValue: new DateTime(1, 1, 1, 0, 0, 0, 0, DateTimeKind.Unspecified));

            migrationBuilder.AddColumn<string>(
                name: "description",
                table: "party",
                type: "character varying(500)",
                maxLength: 500,
                nullable: false,
                defaultValue: "");

            migrationBuilder.AddColumn<string>(
                name: "name",
                table: "party",
                type: "character varying(150)",
                maxLength: 150,
                nullable: false,
                defaultValue: "");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "abbreviation",
                table: "party");

            migrationBuilder.DropColumn(
                name: "created_at",
                table: "party");

            migrationBuilder.DropColumn(
                name: "description",
                table: "party");

            migrationBuilder.DropColumn(
                name: "name",
                table: "party");

            migrationBuilder.RenameColumn(
                name: "allowed",
                table: "party",
                newName: "status");

            migrationBuilder.AddColumn<string>(
                name: "party_description",
                table: "party",
                type: "text",
                nullable: false,
                defaultValue: "");

            migrationBuilder.AddColumn<string>(
                name: "party_name",
                table: "party",
                type: "text",
                nullable: false,
                defaultValue: "");
        }
    }
}
