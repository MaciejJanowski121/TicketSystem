/**
 * Utility functions for handling comments from different API endpoints
 */

/**
 * Normalize comment data from different API responses to a consistent format
 * @param {Object} comment - Comment object from API
 * @param {string} source - Source of the comment ('ticket' or 'comments') - now both use same format
 * @returns {Object} Normalized comment object
 */
export const normalizeComment = (comment, source = 'auto') => {
  // Both endpoints now return TicketCommentResponse with authorUsername field
  // No normalization needed, but keeping function for consistency
  return {
    comment: comment.comment,
    commentDate: comment.commentDate,
    authorUsername: comment.authorUsername,
    // Keep original fields for backward compatibility
    ...comment
  };
};

/**
 * Normalize an array of comments
 * @param {Array} comments - Array of comment objects
 * @param {string} source - Source of the comments ('ticket' or 'comments')
 * @returns {Array} Array of normalized comment objects
 */
export const normalizeComments = (comments, source = 'auto') => {
  if (!Array.isArray(comments)) {
    return [];
  }

  return comments.map(comment => normalizeComment(comment, source));
};
