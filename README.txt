CS 144 Project 2

Karen Zhang (204 481 438)
Andrew Tran (004 188 159)

1.

Item( ItemID, Name, UserID, Buy_Price, First_Bid, Currently, Number_of_Bids, Started, Ends, Description) 
	Primary Key: ItemID


Category(ItemID, Category)


Bid(Bidder, ItemID, Time, Amount)
	Primary Key: Bidder, ItemID, Time


User(UserID, Rating, Location, Country)
	Primary Key: UserID


"Location/Country" Item's local information. Optionally: Latitude and Longitude 
When Location is a child element of a Bidder, it does not have Latitude and Longitude attributes"


2. nontrivial functional dependencies

3. 