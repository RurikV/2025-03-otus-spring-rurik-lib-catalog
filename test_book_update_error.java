// Test to reproduce book update 500 error
// Testing with null genreIds
var updateDto = new BookUpdateDto("1", "Updated Title", "1", null);

// Testing with empty genreIds  
var updateDto2 = new BookUpdateDto("1", "Updated Title", "1", Set.of());