
async function GetJwtToken() {
    const token = await JSON.parse(sessionStorage.getItem("JWTToken"))
}



async function GetElectionVoters(){
    const token = await GetJwtToken()

    const response = await fetch(
      ("http://localhost:5000/api/GetElectionVoters").searchParams.set('election_id', election_id),
      {
        method: "GET",
        headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
      }
    );

    const data = await response.json();

    if (response.ok) {
      return data.election_voters
    }else{
      throw new Error(data.message);
    }

}