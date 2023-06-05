from flask import Flask, request, jsonify

app = Flask('house_alarm')

# Sample data - you can replace this with your own data source
books = [
    {
        'id': 1,
        'title': 'Book 1',
        'author': 'Author 1'
    },
    {
        'id': 2,
        'title': 'Book 2',
        'author': 'Author 2'
    }
]

# GET method - retrieve all books
@app.route('/books', methods=['GET'])
def get_books():
    return jsonify(books)

# POST method - add a new book
@app.route('/books', methods=['POST'])
def add_book():
    new_book = {
        'id': request.json['id'],
        'title': request.json['title'],
        'author': request.json['author']
    }
    books.append(new_book)
    return jsonify(new_book), 201

# Run the application
if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)

