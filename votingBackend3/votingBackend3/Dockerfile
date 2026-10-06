FROM mcr.microsoft.com/dotnet/aspnet:10.0 AS base

RUN mkdir -p /app/VotingDocuments \
    && chown -R $APP_UID:$APP_UID /app/VotingDocuments

USER $APP_UID

WORKDIR /app
EXPOSE 8080
EXPOSE 8081


# This stage is used to build the service project
FROM mcr.microsoft.com/dotnet/sdk:10.0 AS build
ARG BUILD_CONFIGURATION=Release
WORKDIR /src
COPY ["votingBackend3/votingBackend3.csproj", "votingBackend3/"]
RUN dotnet restore "./votingBackend3/votingBackend3.csproj"
COPY . .
WORKDIR "/src/votingBackend3"
RUN dotnet build "./votingBackend3.csproj" -c $BUILD_CONFIGURATION -o /app/build

# This stage is used to publish the service project to be copied to the final stage
FROM build AS publish
ARG BUILD_CONFIGURATION=Release
RUN dotnet publish "./votingBackend3.csproj" -c $BUILD_CONFIGURATION -o /app/publish /p:UseAppHost=false

# This stage is used in production or when running from VS in regular mode (Default when not using the Debug configuration)
FROM base AS final
WORKDIR /app
COPY --from=publish /app/publish .
ENTRYPOINT ["dotnet", "votingBackend3.dll"]