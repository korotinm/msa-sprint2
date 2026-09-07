import { ApolloServer } from '@apollo/server';
import { startStandaloneServer } from '@apollo/server/standalone';
import { buildSubgraphSchema } from '@apollo/subgraph';
import gql from 'graphql-tag';

const typeDefs = gql`
  type Booking @key(fields: "id") {
    id: ID!
    userId: String!
    hotelId: String!
    promoCode: String
    discountPercent: Int
    hotel: Hotel
  }
  
  type Hotel @key(fields: "id") {
    id: ID!
  }

  type Query {
    bookingsByUser(userId: String!): [Booking]
  }

`;

const resolvers = {
  Query: {
    bookingsByUser: async (_, { userId }, { req }) => {
        const useridAcl = req.headers['userid'];
        const allowed = useridAcl === userId
        console.log(`bookingsByUser: useridAcl=${useridAcl} userId=${userId} --> ${allowed ? 'ALLOW' : 'DENY'}`);

        const caller = req.headers['userid'];
        if(caller === userId) {
          return [
            {
              id: "b1",
              userId: userId,
              hotelId: "h1",
              promoCode: "SUMMER",
              discountPercent: 20,
            },
          ];
        } else {
          return [];
        }

    },
  },
  Booking: {
    hotel: (booking) => {
      return {id: booking.hotelId}
    }
  },
};

const server = new ApolloServer({
  schema: buildSubgraphSchema([{ typeDefs, resolvers }]),
});

startStandaloneServer(server, {
  listen: { port: 4001 },
  context: async ({ req }) => ({ req }),
}).then(() => {
  console.log('✅ Booking subgraph ready at http://localhost:4001/');
});
