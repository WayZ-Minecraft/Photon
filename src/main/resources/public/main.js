document.getElementById('statusButton').addEventListener('click', function() {
    fetch('/status')
        .then(response => response.text())
        .then(data => {
            document.getElementById('statusResult').innerText = data;
        })
        .catch(error => {
            console.error('Error fetching status:', error);
        });
});